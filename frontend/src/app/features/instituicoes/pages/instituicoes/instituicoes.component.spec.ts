import { ComponentFixture, TestBed } from '@angular/core/testing';

import { InstituicoesComponent } from './instituicoes.component';

describe('InstituicoesComponent', () => {
  let component: InstituicoesComponent;
  let fixture: ComponentFixture<InstituicoesComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [InstituicoesComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(InstituicoesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
